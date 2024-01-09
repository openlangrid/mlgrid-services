package org.langrid.mlgridservices.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GpuSpec {
	private int id;
	private int memoryMB;
}
