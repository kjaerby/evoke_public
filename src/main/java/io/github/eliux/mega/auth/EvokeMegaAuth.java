package io.github.eliux.mega.auth;

import io.github.eliux.mega.EvokeMegaSession;
import io.github.eliux.mega.error.MegaLoginException;

public abstract class EvokeMegaAuth extends MegaAuth {

    abstract public EvokeMegaSession login() throws MegaLoginException;
}
