package com.codingsprojects.stack.core.error;

import com.codingsprojects.stack.core.result.Result;
import com.codingsprojects.stack.core.base.ResultResponse;

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
