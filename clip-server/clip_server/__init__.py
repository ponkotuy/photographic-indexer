def dev() -> None:
    import uvicorn

    uvicorn.run("clip_server.main:app", host="0.0.0.0", port=8000, reload=True)


def prod() -> None:
    import os
    import sys

    os.execvp(
        "gunicorn",
        [
            "gunicorn",
            "-k",
            "uvicorn.workers.UvicornWorker",
            "-b",
            "0.0.0.0:8000",
            "clip_server.main:app",
        ],
    )
