package io.github.eliux.mega.cmd;

import io.github.eliux.mega.MegaUtils;
import io.github.eliux.mega.error.MegaIOException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MegaCmdMediaInfo extends AbstractMegaCmdCallerWithParams<List<MediaInfo>> {

    private final Optional<String> remotePath;

    public MegaCmdMediaInfo(String remotePath) {
        this.remotePath = Optional.of(remotePath);
    }

    @Override
    List<String> cmdParams() {
        final List<String> cmdParams = new LinkedList<>();

        remotePath.ifPresent(cmdParams::add);

        return cmdParams;
    }

    @Override
    public String getCmd() {
        return "mediainfo";
    }

    @Override
    public List<MediaInfo> call() {
        try {
            return MegaUtils.handleCmdWithOutput(executableCommandArray())
                    .stream().skip(1)
                    .map(MediaInfo::parseMediaInfo)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MegaIOException("Error while exporting " + remotePath);
        }
    }

}
