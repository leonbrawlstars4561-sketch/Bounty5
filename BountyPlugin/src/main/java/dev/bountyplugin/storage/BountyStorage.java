package dev.bountyplugin.storage;

import dev.bountyplugin.bounty.Bounty;

import java.util.Collection;
import java.util.List;

public interface BountyStorage {

    List<Bounty> load();

    void save(Collection<Bounty> bounties);
}
