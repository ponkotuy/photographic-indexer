#!/usr/bin/env python
# -*- coding:utf-8 -*-
import io

from PIL import Image
from fastapi import FastAPI, UploadFile, Request, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from clip_tensor import ClipTensor

app = FastAPI()
processor = ClipTensor()


@app.exception_handler(RequestValidationError)
async def handler(request: Request, exc: RequestValidationError):
    print(exc)
    return JSONResponse(content={}, status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)


@app.get("/text")
async def text(q: str):
    return {'tensor': processor.text(q).tolist()}


@app.get("/image")
async def image(file: UploadFile):
    content = await file.read()
    img = Image.open(io.BytesIO(content))
    return {'tensor': processor.image(img).tolist()}
