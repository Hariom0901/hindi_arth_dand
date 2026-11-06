
const API = "/api";

const $ = (id) => document.getElementById(id);

let currentRoom = null;
let currentMemberId = null;
let members = [];

async function request(path, options = {}) {
    const response = await fetch(`${API}${path}`, {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers
        }
    });

    const text = await response.text();
    let data = null;

    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!response.ok) {
        const message =
            typeof data === "string" ? data :
            data?.detail || data?.message || data?.error ||
            `Request failed (${response.status})`;

        throw new Error(message);
    }

    return data;
}

function showMessage(elementId, message, isError = false) {
    const element = $(elementId);
    element.textContent = message;
    element.className = `message ${isError ? "error" : "success"}`;
}

function clearMessage(elementId) {
    $(elementId).textContent = "";
    $(elementId).className = "message";
}

function showActiveRoom(room) {
    currentRoom = room;

    $("active-room-name").textContent = room.name;
    $("active-room-code").textContent = room.roomCode;

    $("active-room").classList.remove("hidden");
    $("members-panel").classList.remove("hidden");
}

function roomPath() {
    return `/rooms/${encodeURIComponent(currentRoom.roomCode)}`;
}

$("create-room-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessage("room-message");

    const name = $("room-name").value.trim();
    if (!name) return;

    try {
        const room = await request("/rooms", {
            method: "POST",
            body: JSON.stringify({ name })
        });

        showActiveRoom(room);
        showMessage("room-message", "Room created successfully!");

        $("join-code").value = room.roomCode;
        await loadMembers();
    } catch (error) {
        showMessage("room-message", error.message, true);
    }
});


$("join-room-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessage("room-message");

    const roomCode = $("join-code").value.trim().toUpperCase();
    const name = $("member-name").value.trim();

    if (!roomCode || !name) return;

    try {
        // Get members already registered in this room.
        const existingMembers = await request(
            `/rooms/${encodeURIComponent(roomCode)}/members`
        );

        // Find your existing member record (case-insensitive).
        let member = existingMembers.find(
            (m) => m.name.trim().toLowerCase() === name.toLowerCase()
        );

        if (member) {
            showMessage("room-message", `Welcome back, ${member.name}!`);
        } else {
            // Register a new member only if the name doesn't exist.
            member = await request(
                `/rooms/${encodeURIComponent(roomCode)}/members`,
                {
                    method: "POST",
                    body: JSON.stringify({ name })
                }
            );

            showMessage("room-message", `Joined successfully as ${member.name}.`);
        }

        currentRoom = {
            roomCode,
            name: roomCode
        };

        $("active-room-name").textContent = currentRoom.name;
        $("active-room-code").textContent = currentRoom.roomCode;
        $("active-room").classList.remove("hidden");
        $("members-panel").classList.remove("hidden");

        currentMemberId = member.id;

        await loadMembers();

    } catch (error) {
        showMessage("room-message", error.message, true);
    }
});

$("copy-code").addEventListener("click", async () => {
    if (!currentRoom) return;

    try {
        await navigator.clipboard.writeText(currentRoom.roomCode);
        showMessage("member-message", "Room code copied!");
    } catch {
        showMessage("member-message", `Room code: ${currentRoom.roomCode}`);
    }
});

$("add-member").addEventListener("click", async () => {
    clearMessage("member-message");

    if (!currentRoom) return;

    const name = $("new-member-name").value.trim();
    if (!name) {
        showMessage("member-message", "Enter a member name first.", true);
        return;
    }

    try {
        await request(`${roomPath()}/members`, {
            method: "POST",
            body: JSON.stringify({ name })
        });

        $("new-member-name").value = "";
        showMessage("member-message", `${name} added to the room.`);
        await loadMembers();
    } catch (error) {
        showMessage("member-message", error.message, true);
    }
});

$("refresh-members").addEventListener("click", loadMembers);

async function loadMembers() {
    if (!currentRoom) return;

    try {
        members = await request(`${roomPath()}/members`);
        $("members-list").replaceChildren();

        if (!members.length) {
            $("members-list").innerHTML =
                '<p class="empty-state">No members yet. Add someone to get started.</p>';
            return;
        }

        for (const member of members) {
            const card = document.createElement("article");
            card.className = "member-card";

            const info = document.createElement("div");

            const name = document.createElement("div");
            name.className = "member-name";
            name.textContent = member.name;

            const balance = document.createElement("div");
            balance.className = "balance";
            balance.textContent = "Loading balance…";

            const label = document.createElement("span");
            label.className = "balance-label";
            label.textContent = " remaining";

            balance.appendChild(label);
            info.append(name, balance);

            const actions = document.createElement("div");
            actions.className = "member-actions";

            const fineButton = makeButton("+ ₹5 Fine", "", async () => {
                await performMemberAction(member, "fine");
            });

            const payButton = makeButton("Pay", "pay-button", async () => {
                await payMember(member);
            });

            const historyButton = makeButton("History", "history-button", async () => {
                await loadHistory(member);
            });

            actions.append(fineButton, payButton, historyButton);
            card.append(info, actions);
            $("members-list").appendChild(card);

            try {
                const data = await request(
                    `${roomPath()}/members/${member.id}/balance`
                );

                balance.textContent = `₹${data.remainingBalance}`;
                balance.appendChild(label);
            } catch {
                balance.textContent = "Balance unavailable";
            }
        }
    } catch (error) {
        showMessage("member-message", error.message, true);
    }
}

function makeButton(text, className, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = text;
    if (className) button.className = className;

    button.addEventListener("click", async () => {
        button.disabled = true;
        try {
            await onClick();
        } catch (error) {
            showMessage("member-message", error.message, true);
        } finally {
            button.disabled = false;
        }
    });

    return button;
}

async function performMemberAction(member, action) {
    const path =
        `${roomPath()}/members/${member.id}/${action === "fine" ? "fines" : "balance"}`;

    if (action !== "fine") return;

    await request(path, { method: "POST" });
    showMessage("member-message", `₹5 fine added for ${member.name}.`);
    await loadMembers();
}

async function payMember(member) {
    const amountText = prompt(`How much is ${member.name} paying in rupees?`);
    if (amountText === null) return;

    const amount = Number(amountText);

    if (!Number.isSafeInteger(amount) || amount <= 0) {
        throw new Error("Enter a positive whole amount in rupees.");
    }

    const result = await request(
        `${roomPath()}/members/${member.id}/payments`,
        {
            method: "POST",
            body: JSON.stringify({ amount })
        }
    );

    showMessage(
        "member-message",
        `Payment recorded. Remaining balance: ₹${result.remainingBalance}.`
    );

    await loadMembers();
}

async function loadHistory(member) {
    const history = await request(
        `${roomPath()}/members/${member.id}/history`
    );

    currentMemberId = member.id;
    $("history-member-name").textContent = member.name;
    $("history-panel").classList.remove("hidden");

    const list = $("history-list");
    list.replaceChildren();

    if (!history.length) {
        list.innerHTML = '<p class="empty-state">No transactions yet.</p>';
    }

    for (const item of history) {
        const row = document.createElement("div");
        row.className = "history-item";

        const details = document.createElement("div");

        const type = document.createElement("div");
        type.className = "history-type";
        type.textContent = item.type === "FINE" ? "Hindi word fine" : "Payment";

        const date = document.createElement("div");
        date.className = "history-date";
        date.textContent = new Date(item.createdAt).toLocaleString();

        details.append(type, date);

        const amount = document.createElement("div");
        amount.className =
            `history-amount ${item.type === "FINE" ? "fine" : "payment"}`;
        amount.textContent =
            `${item.type === "FINE" ? "+" : "−"}₹${item.amount}`;

        row.append(details, amount);
        list.appendChild(row);
    }

    $("history-panel").scrollIntoView({ behavior: "smooth", block: "start" });
}
