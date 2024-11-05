package com.example.application.data;

import java.io.Serializable;
import java.util.List;

public record WebPushOptions(String body,
                             List<WebPushAction> actions,
                             Serializable data,
                             String icon) implements Serializable {
}