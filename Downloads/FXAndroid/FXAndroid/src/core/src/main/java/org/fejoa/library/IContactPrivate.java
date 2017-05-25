/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.library;

import org.fejoa.library.crypto.CryptoException;


public interface IContactPrivate extends IContactPublic {
    byte[] sign(SigningKeyPair signingKeyPair, byte data[]) throws CryptoException;
}
