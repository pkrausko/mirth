package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RDF extends Segment {
	public _RDF(){
		fields = new Class[]{_NM.class, _RCD.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Number of Columns Per Row", "Column Description"};
		description = "Table Row Definition";
		name = "RDF";
	}
}
