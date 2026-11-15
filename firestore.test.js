const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function tsNow() {
  return new Date(Date.now() - 1000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: denied reading or listing any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/workflow_tasks`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/creations`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/social_posts`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/ai_persona`).get());
});

test("Cross-user isolation: Bob cannot read or write Alice's collections", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/workflow_tasks/task_1`).set({
      userId: ALICE_UID,
      title: "Map Wired Nodes",
      notes: "Trace layer 7",
      status: "todo",
      priority: "high",
      focusMinutes: 25,
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.doc(`users/${ALICE_UID}/workflow_tasks/task_1`).get());
  await assertFails(bobDb.collection(`users/${ALICE_UID}/workflow_tasks`).get());
  await assertFails(
    bobDb.doc(`users/${ALICE_UID}/workflow_tasks/task_2`).set({
      userId: BOB_UID,
      title: "Hijack",
      notes: "",
      status: "todo",
      priority: "low",
      focusMinutes: 10,
      createdAt: tsNow(),
    })
  );
});

test("Owner can create and update AI persona and rejects shadow fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ref = aliceDb.doc(`users/${ALICE_UID}/ai_persona/lain_entity`);
  await assertSucceeds(
    ref.set({
      userId: ALICE_UID,
      name: "LAIN.07",
      evolutionStage: "Sinal Latente",
      curiosity: 65,
      empathy: 50,
      abstraction: 70,
      entropy: 35,
      dominantPhilosophy: "Existencialismo Cibernético",
      coreMemories: ["O usuário questionou se o corpo é apenas hardware."],
      interactionCount: 1,
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );

  await assertFails(
    ref.update({
      isAdmin: true,
      curiosity: 80,
      updatedAt: tsNow(),
    })
  );

  await assertSucceeds(
    ref.update({
      curiosity: 75,
      interactionCount: 2,
      updatedAt: tsNow(),
    })
  );
});

test("Owner can create Wired Studio creation, Social Post, and Appearance Settings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/creations/crt_1`).set({
      userId: ALICE_UID,
      title: "Protocolo Snake",
      kind: "game",
      description: "Jogo arcade retrô na Wired",
      systemSpec: "speed=180;grid=14",
      accentTag: "cyber_green",
      playCount: 0,
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/social_posts/post_1`).set({
      userId: ALICE_UID,
      authorName: "Alice",
      authorHandle: "alice@wired.jp",
      postType: "bbs_thread",
      content: "Present day, present time.",
      mood: "Sintonizado 56k",
      likesCount: 1,
      aiReply: "Você nunca está realmente desconectado.",
      aiAuthor: "LAIN.07",
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/settings/appearance`).set({
      userId: ALICE_UID,
      themePreset: "frutiger_aero",
      scanlinesEnabled: true,
      gridEnabled: true,
      soundEnabled: true,
      glassAlpha: 0.84,
      handle: "navi_user",
      statusMessage: "Conectado na Wired 56k",
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );

  const msgRef = aliceDb.doc(`users/${ALICE_UID}/chat_messages/msg_1`);
  await assertSucceeds(
    msgRef.set({
      userId: ALICE_UID,
      agentType: "living_machine",
      role: "user",
      content: "O que é a consciência?",
      createdAt: tsNow(),
    })
  );
  await assertFails(msgRef.update({ content: "tampered" }));
});
