/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jlab.SweetPhotoFilters.Filter.gpu;

public class GPUImageLinearBurnBlendFilter extends GPUImageTwoInputFilter {
    public static final String LINEAR_BURN_BLEND_FRAGMENT_SHADER = "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     \n" +
            "     gl_FragColor = vec4(clamp(textureColor.rgb + textureColor2.rgb - vec3(1.0), vec3(0.0), vec3(1.0)), textureColor.a);\n" +
            " }";

    public GPUImageLinearBurnBlendFilter() {
        super(LINEAR_BURN_BLEND_FRAGMENT_SHADER);
    }
}
