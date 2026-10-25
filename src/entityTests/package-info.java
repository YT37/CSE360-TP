/**
 * Semi-automated console tests for entity behavior that does not need a database, including
 * whole-lesson and field locking and list update/remove semantics. Each case prints its expectation
 * and result, and a non-zero failing count makes the process exit with an assertion error.  These
 * tests cover R4, R7, and R13.
 */
package entityTests;
