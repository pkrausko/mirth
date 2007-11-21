package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA17 extends Message{	
	public _ADTA17(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _DB1.class, _OBX.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _DB1.class, _OBX.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, -1};
		required = new boolean[]{true, true, true, false, true, false, false, false, true, false, true, false, false, false};
		groups = new int[][]{}; 
		description = "Swap Patients";
		name = "ADTA17";
	}
}
