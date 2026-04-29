package com.sysmap.hubapi.service;

import com.sysmap.hubapi.repository.ActivityRepository;
import com.sysmap.hubapi.repository.ActivityTypeRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityTypeRepository activityTypeRepository;

    @InjectMocks
    private ActivityService activityService;

    // TODO: shouldCreateActivity
    // TODO: shouldListActivitiesFilteredByType
    // TODO: shouldListActivitiesPrioritizingUserPreferences
    // TODO: shouldThrowWhenNonCreatorTriesToConclude
    // TODO: shouldDeleteActivity
}
