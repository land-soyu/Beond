/**
 * COPYRIGHT (C) 2017
 * TOSHIBA CORPORATION STORAGE & ELECTRONIC DEVICES SOLUTIONS COMPANY
 * ALL RIGHTS RESERVED
 *
 * THE SOURCE CODE AND ITS RELATED DOCUMENTATION IS PROVIDED "AS IS". TOSHIBA
 * CORPORATION MAKES NO OTHER WARRANTY OF ANY KIND, WHETHER EXPRESS, IMPLIED OR,
 * STATUTORY AND DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF MERCHANTABILITY,
 * SATISFACTORY QUALITY, NON INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * THE SOURCE CODE AND DOCUMENTATION MAY INCLUDE ERRORS. TOSHIBA CORPORATION
 * RESERVES THE RIGHT TO INCORPORATE MODIFICATIONS TO THE SOURCE CODE IN LATER
 * REVISIONS OF IT, AND TO MAKE IMPROVEMENTS OR CHANGES IN THE DOCUMENTATION OR
 * THE PRODUCTS OR TECHNOLOGIES DESCRIBED THEREIN AT ANY TIME.
 *
 * TOSHIBA CORPORATION SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT OR
 * CONSEQUENTIAL DAMAGE OR LIABILITY ARISING FROM YOUR USE OF THE SOURCE CODE OR
 * ANY DOCUMENTATION, INCLUDING BUT NOT LIMITED TO, LOST REVENUES, DATA OR
 * PROFITS, DAMAGES OF ANY SPECIAL, INCIDENTAL OR CONSEQUENTIAL NATURE, PUNITIVE
 * DAMAGES, LOSS OF PROPERTY OR LOSS OF PROFITS ARISING OUT OF OR IN CONNECTION
 * WITH THIS AGREEMENT, OR BEING UNUSABLE, EVEN IF ADVISED OF THE POSSIBILITY OR
 * PROBABILITY OF SUCH DAMAGES AND WHETHER A CLAIM FOR SUCH DAMAGE IS BASED UPON
 * WARRANTY, CONTRACT, TORT, NEGLIGENCE OR OTHERWISE.
 */

package jp.co.toshiba.semicon.sapp01.Function;

import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class OtaUtil {


    /** 
     *  Parse one line of Intel HEX file
     *  hexadecimal characters that encode multiple binary numbers -> byte array
     */
    private static final Pattern PATTERN_2CHAR = Pattern.compile("([0-9a-fA-F]{2})");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static byte[] str2hex(String str) {

        if (TextUtils.isEmpty(str)) {
            return null;
        }
        str.replace("\r\n",""); // delete CRLF
        str.replace(":","");    // delete colon

        int capacity = (str.length() / 2);
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.clear();

        Matcher m = PATTERN_2CHAR.matcher(str);
        while (m.find()) {
            String part = str.substring(m.start(), m.end());
            buffer.put((byte) (Short.parseShort(part, 16) & 0xFF));
        }

        return buffer.array();
    }


    static boolean isOk1LineChecksum(byte[] data) {

        int sum = 0;

        for (byte aData : data) {
            sum += (int) aData & 0xff;
        }
        sum &= 0xff;

        return sum == 0;
    }

}
