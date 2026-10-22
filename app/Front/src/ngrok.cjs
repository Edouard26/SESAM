
const ngrok = require("@ngrok/ngrok");

async function startTunnel() {
    const tunnel = await ngrok.forward({
        addr: "localhost:5173",
        authtoken_from_env: true
    });

    console.log("Site accessible sur :", tunnel.url());
}

startTunnel().catch(console.error);
