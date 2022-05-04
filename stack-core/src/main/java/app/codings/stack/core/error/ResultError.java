package app.codings.stack.core.error;

import app.codings.stack.core.result.Result;
import app.codings.stack.core.base.ResultResponse;

public final class ResultError extends RuntimeException
{
    private final ResultResponse body;

    public ResultError(Result result)
    {
        this.body = ResultResponse.of(result);
    }

    public ResultResponse getBody()
    {
        return body;
    }
}
