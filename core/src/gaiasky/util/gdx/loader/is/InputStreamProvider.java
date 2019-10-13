/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.util.gdx.loader.is;

import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamProvider {

    InputStream getInputStream(FileHandle f) throws IOException;
}
