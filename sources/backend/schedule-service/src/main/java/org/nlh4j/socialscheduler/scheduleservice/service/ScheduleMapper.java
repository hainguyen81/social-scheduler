package org.nlh4j.socialscheduler.scheduleservice.service;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleResponseDto;
import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ScheduleMapper {

	ScheduleResponseDto toResponseDto(ScheduleEntity entity);
}
