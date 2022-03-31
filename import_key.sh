#!/bin/bash

echo "$1" > b64_private.key
echo "$2" > b64_public.key
base64 -d b64_private.key > private.key
base64 -d b64_public.key > public.key
gpg --pinentry-mode=loopback --passphrase "$3" --import private.key
gpg --pinentry-mode=loopback --passphrase "$3" --import public.key