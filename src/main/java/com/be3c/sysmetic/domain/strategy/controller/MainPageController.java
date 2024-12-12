package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.dto.MainPageAnalysisDto;
import com.be3c.sysmetic.domain.strategy.dto.MainPageDto;
import com.be3c.sysmetic.domain.strategy.service.MainPageService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/main")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class MainPageController implements MainPageControllerDocs {

    private final MainPageService mainPageService;

    /*
        getMainPage : 메인 페이지 요청
    */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/info")
    public APIResponse<MainPageDto> getMainPage() {

        MainPageDto m = mainPageService.getMain();

        return APIResponse.success(m);
    }


    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/analysis")
    public APIResponse<MainPageAnalysisDto> getMainGraph() {

        MainPageAnalysisDto analysis = mainPageService.getAnalysis();

        return APIResponse.success(analysis);
    }
}
