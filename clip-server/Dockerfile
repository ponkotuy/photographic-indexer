FROM python:3.11.3-slim-bullseye

ENV POETRY_HOME=/opt/poetry
ENV POETRY_VERSION=1.8.5
ENV HF_HOME=/app/.cache/huggingface

RUN apt-get update && \
    apt-get install -y curl pkg-config git && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN curl -sSL https://install.python-poetry.org | python3 - && \
    cd /usr/local/bin && \
    ln -s /opt/poetry/bin/poetry

WORKDIR /app

COPY ./poetry.lock ./pyproject.toml /app/

RUN poetry install --no-dev --no-root

# Pre-download the model during build
RUN poetry run python -c "from transformers import AutoImageProcessor, AutoModel, AutoTokenizer; \
    AutoTokenizer.from_pretrained('line-corporation/clip-japanese-base', trust_remote_code=True); \
    AutoImageProcessor.from_pretrained('line-corporation/clip-japanese-base', trust_remote_code=True); \
    AutoModel.from_pretrained('line-corporation/clip-japanese-base', trust_remote_code=True)"

COPY *.py /app/

CMD poetry run gunicorn -k uvicorn.workers.UvicornWorker -b 0.0.0.0:8000 main:app
