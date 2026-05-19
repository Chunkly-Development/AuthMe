package org.chunkly.authme.auth.mojang;

import java.util.Optional;

public record MojangOwnershipResult(boolean premium, boolean owner, Optional<MojangProfile> profile) {

    public static MojangOwnershipResult noPremium() {
        return new MojangOwnershipResult(false, false, Optional.empty());
    }

    public static MojangOwnershipResult premiumButNotOwner(MojangProfile profile) {
        return new MojangOwnershipResult(true, false, Optional.of(profile));
    }

    public static MojangOwnershipResult verifiedOwner(MojangProfile profile) {
        return new MojangOwnershipResult(true, true, Optional.of(profile));
    }
}
