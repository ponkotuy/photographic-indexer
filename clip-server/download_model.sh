#!/bin/sh

cd `dirname $0`

MODEL_NAME=japanese-cloob-vit-b-16

mkdir $MODEL_NAME
cd $MODEL_NAME
curl https://huggingface.co/rinna/japanese-cloob-vit-b-16/resolve/main/pytorch_model.bin -o pytorch_model.bin
curl https://huggingface.co/rinna/japanese-cloob-vit-b-16/resolve/main/config.json -o config.json
