package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.event.dto.LocationDto;
import ru.practicum.event.model.Location;

@UtilityClass
public class LocationMapper {
    public LocationDto toLocationDto(Location location) {
        return new LocationDto(
                location.getLat(),
                location.getLon()
        );
    }

    public Location toLocation(LocationDto locationDto) {
        return new Location(locationDto.getLat(),
                locationDto.getLon()
        );
    }
}
