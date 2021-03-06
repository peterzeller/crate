==================
Unreleased Changes
==================

.. NOTE::

    These changes have not yet been released.

    If you are viewing this document on the Crate.io website, these changes
    reflect what exists on `the master branch`_ in Git. This is where we
    collect changes before they are ready for release.

.. WARNING::

    Unreleased changes may not be ready for general use and could lead to data
    corruption or data loss. You should `back up your data`_ before
    experimenting with unreleased changes.

.. _the master branch: https://github.com/crate/crate
.. _back up your data: https://crate.io/docs/crate/reference/en/latest/admin/snapshots.html

.. DEVELOPER README
.. ================

.. Changes should be recorded here as you are developing CrateDB. When a new
.. release is being cut, changes will be moved to the appropriate release notes
.. file.

.. When resetting this file during a release, leave the headers in place, but
.. add a single paragraph to each section with the word "None".

.. Always cluster items into bigger topics. Link to the documentation whenever feasible.
.. Remember to give the right level of information: Users should understand
.. the impact of the change without going into the depth of tech.

.. rubric:: Table of contents

.. contents::
   :local:


Breaking Changes
================

None


Deprecations
============

None

Changes
=======

- Improved the performance of queries on the ``sys.health`` table.

- Added support for the ``dense_rank`` window function, which is available as an
  enterprise feature.

- Added support for the ``rank`` window function, which is available as an
  enterprise feature.

- Added the ``delimiter`` option for :ref:`copy_from` CSV files. The option is
  used to specify the character that separates columns within a row.

- Added the ``empty_string_as_null`` option for :ref:`copy_from` CSV files.
  If the option is enabled, all column's values represented by an empty string,
  including a quoted empty string, are set to ``NULL``.

- Added the :ref:`sys.snapshot_restore <sys-snapshot-restore>` table to track the
  progress of the :ref:`snapshot restore <snapshot-restore>` operations.

- Added support for using the optimized primary key lookup plan if additional
  filters are combined via ``AND`` operators.

- Improved the performance of queries on the ``sys.allocations`` table in cases
  where there are filters restricting the result set or if only a sub-set of
  the columns is selected.

Fixes
=====

- Fixed an issue that resulted in records in ``pg_catalog.pg_proc`` which
  wouldn't be joined with ``pg_catalog.pg_type``. Clients like ``npgsql`` use
  this information and without it the users received an error like ``The CLR
  array type System.Int32[] isn't supported by Npgsql or your PostgreSQL`` if
  using array types.

- Fixed an issue that could lead to stuck ``INSERT INTO .. RETURNING`` queries.

- Fixed a regression introduced in CrateDB >= ``4.3`` which prevents using
  ``regexp_matches()`` wrapped inside a subscript expression from being used
  as a ``GROUP BY`` expression.
  This fixed the broken AdminUI->Montoring tab as it uses such an statement.

- Fixed validation of ``GROUP BY`` expressions if an alias is used. The
  validation was by passed and resulted in an execution exception instead of
  an user friendly validation exception.

- Fixed an issue that caused ``IS NULL`` and ``IS NOT NULL`` operators on
  columns of type ``OBJECT`` with the column policy ``IGNORED`` to match
  incorrect records.

- Fixed an issue that led to an error like ``UnsupportedOperationException:
  Can't handle Symbol [ParameterSymbol: $1]`` if using ``INSERT INTO`` with a
  query that contains parameter place holders and a ``LIMIT`` clause.

- Fixed an issue that led to an error if a user nested multiple table
  functions.
