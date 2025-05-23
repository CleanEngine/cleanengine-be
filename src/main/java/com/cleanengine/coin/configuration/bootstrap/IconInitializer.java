package com.cleanengine.coin.configuration.bootstrap;

import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class IconInitializer implements ApplicationRunner {
    private final AssetRepository assetRepository;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Asset> assets = loadAssets();

        for(Asset asset : assets){
            if(asset.getIcon() != null) continue;
            byte[] encodedIconBytes = loadEncodedIcon(asset.getTicker());
            asset.setIcon(encodedIconBytes);
            assetRepository.save(asset);
        }
    }

    private List<Asset> loadAssets(){
        return assetRepository.findAll();
    }

    public byte[] loadEncodedIcon(String ticker){
        Resource resource = getResource(ticker);
        String svgContent = convertToStr(resource);
        String base64Str = convertToBase64(svgContent);
        return base64Str.getBytes(StandardCharsets.UTF_8);
    }

    private String convertToStr(Resource resource){
        String svgContent;
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            svgContent = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return svgContent;
    }

    private Resource getResource(String ticker){
        Resource resource = resourceLoader.getResource("classpath:icons/" + ticker + ".svg");
        if(!resource.exists()){
            log.debug("Icon not found. with " + ticker);
        }
        return resource;
    }

    private String convertToBase64(String svgStr){
        return Base64.getEncoder().encodeToString(svgStr.getBytes(StandardCharsets.UTF_8));
    }
}
