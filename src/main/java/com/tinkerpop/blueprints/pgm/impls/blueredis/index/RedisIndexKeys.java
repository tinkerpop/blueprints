/******************************************************************************
 * Copyright (c) 2010-2011. Dmitrii Dimandt <dmitrii@dmitriid.com>            *
 *                                                                            *
 *   Licensed under the Apache License, Version 2.0 (the "License");          *
 *   you may not use this file except in compliance with the License.         *
 *   You may obtain a copy of the License at                                  *
 *                                                                            *
 *       http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                            *
 *   Unless required by applicable law or agreed to in writing, software      *
 *   distributed under the License is distributed on an "AS IS" BASIS,        *
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *   See the License for the specific language governing permissions and      *
 *   limitations under the License.                                           *
 ******************************************************************************/

package com.tinkerpop.blueprints.pgm.impls.blueredis.index;

public class RedisIndexKeys {
    public static final String AUTO   = "index:auto:";
    public static final String MANUAL = "index:manual:";

    public static final String META_AUTO   = "index:meta:auto:";
    public static final String META_MANUAL = "index:meta:manual:";

    public static final String META_INDICES_AUTO   = "index:meta:indices:auto";
    public static final String META_INDICES_MANUAL = "index:meta:indices:manual";
}
