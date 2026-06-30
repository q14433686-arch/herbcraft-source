package com.herbcraft.knowledge.client;

import com.herbcraft.knowledge.CodexSnapshotPayload;

/**
 * Small client-side cache for the most recent Codex snapshot.
 *
 * <p>This lets snapshot packets remain pure data while a separate explicit payload decides whether
 * the Codex UI should open.</p>
 */
public final class CodexClientState {
   private static CodexSnapshotPayload latestSnapshot;

   private CodexClientState() {
   }

   public static void setLatestSnapshot(CodexSnapshotPayload snapshot) {
      latestSnapshot = snapshot;
   }

   public static CodexSnapshotPayload latestSnapshot() {
      return latestSnapshot;
   }

   public static void clear() {
      latestSnapshot = null;
   }
}
