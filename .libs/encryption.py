# requirements: `pip install cryptography`
import base64
import os

from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC


def generate_key_from_password(password: str, salt: bytes) -> bytes:
    """Derives a secure cryptographic key from a user-provided password using PBKDF2.

    Args:
        password: The plain-text password string provided by the user.
        salt: A cryptographically secure random byte string used to secure the KDF.

    Returns:
        A URL-safe base64-encoded 32-byte key suitable for Fernet encryption.
    """
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt,
        iterations=480_000, # Prevents brute-force attacks by slowing down the derivation
    )
    return base64.urlsafe_b64encode(kdf.derive(password.encode()))

def encrypt_data(data: str, password: str) -> tuple[bytes, bytes]:
    """Encrypts a plain-text string using a key derived from a password.

    Args:
        data: The sensitive text message that needs to be encrypted.
        password: The master password string used to secure the data.

    Returns:
        A tuple containing:
            - bytes: The secure encrypted ciphertext.
            - bytes: The unique salt generated for this encryption session.
    """
    salt = os.urandom(16) # Unique salt per encryption ensures distinct ciphertexts
    key = generate_key_from_password(password, salt)
    fernet = Fernet(key)
    
    ciphertext = fernet.encrypt(data.encode())
    return ciphertext, salt

def decrypt_data(ciphertext: bytes, password: str, salt: bytes) -> str:
    """Decrypts an encrypted byte string using the password and the original salt.

    Args:
        ciphertext: The encrypted byte data that needs to be decrypted.
        password: The password string that was used during encryption.
        salt: The exact unique salt bytes generated during encryption.

    Returns:
        The original decrypted plain-text string.

    Raises:
        InvalidToken: If the password/salt is wrong, or if the data was tampered with.
    """
    key = generate_key_from_password(password, salt)
    fernet = Fernet(key)
    
    decrypted_text = fernet.decrypt(ciphertext)
    return decrypted_text.decode()
