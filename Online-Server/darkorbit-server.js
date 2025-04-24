/**
 * WebSocket Server for Multiplayer and Mob Management
 *
 * This server allows players to connect via WebSocket, update their positions in real-time,
 * and receive updates about other players' positions and server-controlled mobs.
 *
 * Technologies Used:
 * - Node.js
 * - Express.js
 * - WebSocket (via the `ws` library)
 *
 * Features:
 * 1. Handles player connections and disconnections.
 * 2. Synchronizes player positions across all connected clients.
 * 3. Manages mobs' positions and behaviors on the server.
 * 4. Broadcasts both player and mob updates to all clients.
 * 5. Provides a basic HTTP route for server status.
 *
 * Setup Instructions:
 * 1. Ensure Node.js is installed.
 * 2. Install dependencies using `npm install express ws`.
 * 3. Start the server using `node darkorbit-server.js`.
 */

const express = require("express");
const { WebSocketServer } = require("ws");
const path = require("path");

const app = express();
const PORT = 3000;

const server = app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});

const wss = new WebSocketServer({ server });

const players = {};
const mobs = {}; // Store mobs and their positions

// Initialize mobs
const MOB_COUNT = 5;
for (let i = 1; i <= MOB_COUNT; i++) {
  mobs[`mob${i}`] = { x: Math.random() * 500, y: Math.random() * 500 };
}

// Function to broadcast data to all clients
function broadcast(data) {
  const message = JSON.stringify(data);
  wss.clients.forEach((client) => {
    if (client.readyState === client.OPEN) {
      client.send(message);
    }
  });
}

// Function to update mobs' positions
function updateMobs() {
  for (const mobId in mobs) {
    mobs[mobId].x += Math.random() * 10 - 5; // Random movement
    mobs[mobId].y += Math.random() * 10 - 5;
  }
  broadcast({ type: "mobs", mobs });
}

// Update mobs every second
setInterval(updateMobs, 1000);

wss.on("connection", (ws) => {
  console.log("A player connected");

  ws.on("message", (message) => {
    try {
      const data = JSON.parse(message);
      if (data.type === "join") {
        players[data.id] = { x: 0, y: 0 };
        console.log(`Player joined: ${data.id}`);
      } else if (data.type === "update") {
        if (players[data.id]) {
          players[data.id] = { x: data.x, y: data.y };
        }
      } else if (data.type === "leave") {
        if (players[data.id]) {
          console.log(`Player left: ${data.id}`);
          delete players[data.id];
        }
      }

      broadcast({ type: "positions", players });
    } catch (error) {
      console.error("Error processing message:", error);
    }
  });

  ws.on("close", () => {
    for (const id in players) {
      if (players.hasOwnProperty(id) && players[id].ws === ws) {
        console.log(`Player disconnected: ${id}`);
        delete players[id];
        break;
      }
    }

    broadcast({ type: "positions", players });
  });
});

app.get("/", (req, res) => {
  res.json({
    message: "WebSocket server is running. Connect to synchronize positions.",
    players: players,
    mobs: mobs,
  });
});

app.get("/documentation", (req, res) => {
  res.sendFile(path.join(__dirname, "documentation.html"));
});