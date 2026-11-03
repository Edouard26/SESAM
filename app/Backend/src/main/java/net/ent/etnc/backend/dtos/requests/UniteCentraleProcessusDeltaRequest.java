package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UniteCentraleProcessusDeltaRequest {
    private List<String> started = new ArrayList<>();
    private List<String> stopped = new ArrayList<>();
}
