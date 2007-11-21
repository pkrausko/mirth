package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFKM05 extends Message{	
	public _MFKM05(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _MFI.class, _MFA.class};
		repeats = new int[]{0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, false, true, false, true, false};
		groups = new int[][]{}; 
		description = "Patient Location Master File - Response";
		name = "MFKM05";
	}
}
