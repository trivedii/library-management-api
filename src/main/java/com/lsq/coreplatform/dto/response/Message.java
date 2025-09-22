package com.lsq.coreplatform.dto.response;

public enum Message {
    SAVE_SUCCESSFUL("Save successful"),
    UPDATE_SUCCESSFUL("Update successful"),
    NO_UPDATE_REQUIRED( "No update Required"),
    DELETE_SUCCESSFUL("Delete successful");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
