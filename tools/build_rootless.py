#!/usr/bin/env python3
"""Embed this module in a user-supplied Jiaowoban APK; never touches a phone."""
import argparse
import hashlib
import os
from pathlib import Path
import re
import secrets
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PATCHER_SHA256 = "d238fdc414d121b7fa454d8b4ccf420df3a8c97d563761861ff92bd9c5da2165"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("original", type=Path)
    parser.add_argument("patcher", type=Path, help="JingMatrix LSPatch v1.2-487 release JAR")
    args = parser.parse_args()
    original, patcher = args.original.resolve(), args.patcher.resolve()
    if hashlib.sha256(patcher.read_bytes()).hexdigest() != PATCHER_SHA256:
        parser.error("Patcher SHA-256 does not match the pinned official release")
    java_home = os.environ.get("JAVA_HOME")
    java = str(Path(java_home) / "bin/java") if java_home else shutil.which("java")
    if not java:
        parser.error("Java 17 or 21 is required; set JAVA_HOME")
    os.environ["PATH"] = str(Path(java).resolve().parent) + os.pathsep + os.environ["PATH"]
    keytool = str(Path(java).resolve().parent / "keytool")
    sdk = Path(os.environ.get("ANDROID_HOME", Path.home() / "Android/Sdk"))
    build_tools = sdk / "build-tools/36.0.0"
    badging = subprocess.check_output(
        [str(build_tools / "aapt"), "dump", "badging", str(original)], text=True
    )
    if not badging.startswith("package: name='edu.sjtu.infoplus.taskcenter' "):
        parser.error("Input is not Jiaowoban (edu.sjtu.infoplus.taskcenter)")
    module = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
    if not module.is_file():
        parser.error("Build the module with ./gradlew :app:assembleDebug first")
    signing = ROOT / "artifacts/rootless-inputs/signing"
    signing.mkdir(parents=True, exist_ok=True, mode=0o700)
    password_file = signing / "password.txt"
    keystore = signing / "jiaowoban-rootless.p12"
    if not password_file.exists():
        with open(password_file, "x", opener=lambda path, flags: os.open(path, flags, 0o600)) as f:
            f.write(secrets.token_urlsafe(32))
    password = password_file.read_text().strip()
    if not keystore.exists():
        subprocess.run([
            keytool, "-genkeypair", "-keystore", str(keystore), "-storetype", "PKCS12",
            "-storepass:env", "JIAOWOBAN_SIGNING_PASSWORD", "-alias", "rootless",
            "-keyalg", "RSA", "-keysize", "3072", "-validity", "10000",
            "-dname", "CN=Jiaowoban Dark Personal Build", "-noprompt",
        ], env={**os.environ, "JIAOWOBAN_SIGNING_PASSWORD": password}, check=True)
        keystore.chmod(0o600)
    output = ROOT / "artifacts/rootless"
    output.mkdir(parents=True, exist_ok=True)
    # Integrated mode: no --manager. Level 2 retains the original signer as seen
    # inside the app; Android still sees our new signer for installation.
    subprocess.run([
        java, "-jar", str(patcher), str(original), "--embed", str(module),
        "--sigbypasslv", "2", "--keystore", str(keystore), password, "rootless", password,
        "--output", str(output), "--force",
    ], check=True)
    generated = output / (original.stem + "-487-lspatched.apk")
    subprocess.run([str(build_tools / "apksigner"), "verify", str(generated)], check=True)
    aligned = output / "aligned.apk"
    subprocess.run([str(build_tools / "zipalign"), "-f", "-P", "16", "4", str(generated),
                    str(aligned)], check=True)
    subprocess.run([
        str(build_tools / "apksigner"), "sign", "--ks", str(keystore), "--ks-key-alias", "rootless",
        "--ks-pass", "env:JIAOWOBAN_SIGNING_PASSWORD", "--key-pass", "env:JIAOWOBAN_SIGNING_PASSWORD",
        "--alignment-preserved", "true",
        str(aligned),
    ], env={**os.environ, "JIAOWOBAN_SIGNING_PASSWORD": password}, check=True)
    subprocess.run([str(build_tools / "apksigner"), "verify", str(aligned)], check=True)
    subprocess.run([str(build_tools / "zipalign"), "-c", "-P", "16", "4", str(aligned)], check=True)
    # Derive the filename from the actual version rather than assuming the input.
    version = re.search(r"versionName='([^']+)'", badging).group(1)
    version = re.sub(r"[^a-zA-Z0-9._-]", "_", version)
    final = output / f"jiaowoban-{version}-dark-rootless.apk"
    aligned.replace(final)
    generated.unlink()
    aligned.with_suffix(".apk.idsig").unlink(missing_ok=True)
    digest = hashlib.sha256(final.read_bytes()).hexdigest()
    (output / "SHA256SUMS").write_text(f"{digest}  {final.name}\n")
    print(f"Created: {final}\nSHA-256: {digest}")


if __name__ == "__main__":
    main()
