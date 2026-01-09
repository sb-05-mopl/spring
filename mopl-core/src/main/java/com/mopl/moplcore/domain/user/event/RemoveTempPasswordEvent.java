package com.mopl.moplcore.domain.user.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemoveTempPasswordEvent {
	private UUID userId;
}
