/**
 * Owns the H2 connection and every prepared SQL statement in the Lessons Learned System. The
 * package persists users, lessons learned, and experience records, enforces lesson ownership and
 * locking, and returns human-readable error strings so the GUI can show the same messages that the
 * database layer relies on.
 */
package database;
