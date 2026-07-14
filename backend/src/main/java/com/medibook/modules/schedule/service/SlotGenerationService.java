package com.medibook.modules.schedule.service;

import java.util.List;

import com.medibook.modules.schedule.dto.request.SlotGenerateRequest;
import com.medibook.modules.schedule.dto.response.SlotResponse;

public interface SlotGenerationService {

    List<SlotResponse> generate(SlotGenerateRequest request);

}
