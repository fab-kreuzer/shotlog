package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.dto.DisagResult;

public interface DisagQrService {

    /** Decodes the QR link through the n8n webhook. Throws {@link IllegalArgumentException} for bad links
     *  and {@link IllegalStateException} for upstream problems; messages are message keys. */
    DisagResult decode(String qrUrl);

    /** Builds an unsaved session (decimal scoring, series of 10, local time) from a decoded result. */
    Session toSession(DisagResult result);
}
