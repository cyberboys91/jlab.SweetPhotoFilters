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

public class GPUImageLightenBlendFilter extends GPUImageTwoInputFilter {
    public static final String LIGHTEN_BLEND_FRAGMENT_SHADER = "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    lowp vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "    \n" +
            "    gl_FragColor = max(textureColor, textureColor2);\n" +
            " }";

    public GPUImageLightenBlendFilter() {
        super(LIGHTEN_BLEND_FRAGMENT_SHADER);
    }
}
