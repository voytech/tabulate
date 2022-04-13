#!/bin/bash

for tag in $(git tag | grep "alpha"); do
    echo "deleting stale tag: $tag"
    git tag -d "$tag"
    git push --delete origin "$tag"
done