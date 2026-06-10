# Plan: Web Dashboard → Discord Channel Provisioning

## Goal
Allow requests to be created from the web dashboard while maintaining full parity with Discord (each request has its own Discord channel).

## Flow

1. Web dashboard calls `POST /api/requests/provision` with request fields (title, description, type, posting date, etc.)
2. Backend uses JDA (already embedded in Spring Boot) to:
   - Create the Discord text channel (placed in the request category)
   - Post the initial embed/message to it
   - Collect the real `channelID` and `mainMessageID`
3. Backend persists a fully-formed `Request` record in one shot — no partial inserts, `@NotNull` constraints satisfied from the start
4. Bot detects the new channel via its existing Discord event listeners and starts handling interactions (status updates, assignments, etc.) as normal — no special setup logic needed

## Key Points

- The bot does **not** need to know whether a request originated from Discord or the web. It just listens.
- No channel setup logic needs to be duplicated in Java — JDA only handles creation and the initial message post.
- The Python bot remains the REST API client it already is (JWT auth already wired up).
- Keep the bot on the REST API (not direct DB access) so audit logging stays centralized and schema changes don't break the bot.

## What Needs to Be Built

- A JDA-backed service method that creates the channel, posts the embed, and returns the IDs
- `POST /api/requests/provision` controller endpoint that calls that service, then delegates to `RequestService.createRequest()`
- Web dashboard UI for the create/edit form (separate concern)

## Edit Parity (web → Discord)

When a request is edited via the web dashboard, the service should also update the Discord embed message via JDA (edit the existing message in the channel). Bot-side edits already flow through the REST API so that path is covered.
