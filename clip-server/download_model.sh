#!/bin/sh

# Pre-download the model for local development
# For Docker builds, this is done automatically in Dockerfile

cd `dirname $0`

python -c "from transformers import AutoImageProcessor, AutoModel, AutoTokenizer; \
    AutoTokenizer.from_pretrained('line-corporation/clip-japanese-base', trust_remote_code=True); \
    AutoImageProcessor.from_pretrained('line-corporation/clip-japanese-base', trust_remote_code=True); \
    AutoModel.from_pretrained('line-corporation/clip-japanese-base', trust_remote_code=True)"
