package com.nan.server.param;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TestResponse {

    private String myName;

    private String myParam;
}
