package com.example.application.data;

import java.io.Serializable;

public record WebPushAction(String action, String title) implements Serializable {
}
