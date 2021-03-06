/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package cubesystem;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;

public class CubeXForm extends Group {

    private final Rotate rx = new Rotate(0, Rotate.X_AXIS);
    private final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rz = new Rotate(0, Rotate.Z_AXIS);

    public CubeXForm() {
        super();
        getTransforms().addAll(rz, ry, rx);
    }

	public Rotate getRx() {
		return rx;
	}

	public void setRx(Rotate value) {
	    copy(rx, value);
	}
	
	public void setRy(Rotate value) {
	    copy(ry, value);
	}
	public Rotate getRy() {
		return ry;
	}

	public Rotate getRz() {
		return rz;
	}


    public void setRz(Rotate value) {
        copy(rz, value);
    }

    public static void copy(Rotate rz2, Rotate value) {
        rz2.setAngle(value.getAngle());
        rz2.setAxis(value.getAxis());
        rz2.setOnTransformChanged(value.getOnTransformChanged());
        rz2.setPivotX(value.getPivotX());
        rz2.setPivotY(value.getPivotY());
        rz2.setPivotZ(value.getPivotZ());
    }

}