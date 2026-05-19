package com.intranet.cic.configs;

import com.intranet.cic.dtos.UpdateUserDTO;
import com.intranet.cic.entities.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // ✅ Force active to always map even when false
        modelMapper.typeMap(UpdateUserDTO.class, User.class)
                .addMappings(mapper -> mapper.map(UpdateUserDTO::getActive, User::setActive));

        return modelMapper;
    }
}
