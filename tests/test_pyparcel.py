"""
Unit tests for the parcelupdate package.

🛑 Under no circumstance should tests connect to the production database. 🛑

The test suite assumes you have an up to date local copy of cogdb database and writes test_data to the copy.
The database (as of August 2020) is not very large, so a subset of the data is not provided.
A link to database dumps can be provided to interested contributors.
"""

#   A note on multi-leveled classes.
#   Some test classes may only contain a single test class.
#   This is intentional:
#       It allows for more test classes to be added under the umbrella of an outer class without refactoring

# Todo: Custom Black config for 3 lines after module level classes, or just ignore file.
# Todo: Get pre-commit working for new IDE. Have tests run automatically on commit

import sys
import pytest
from copy import copy
import psycopg2
from os import path
from contextlib import contextmanager
import functools
from pyparcel._events import *
from pyparcel import _parse
from pyparcel import _write as write
from pyparcel._parse import TaxStatus
from unittest import mock
import pickle


### Fixtures (and similar bits of setup code)

HERE = path.abspath(path.dirname(__file__))
MOCKS = path.join(HERE, "mocks", "")  # Represents the mocks folder

# Generates a list of every eventcategory class in _events
event_categories = []
d = copy(sys.modules[__name__].__dict__)
for k in d:
    try:
        if issubclass(d[k], Event):
            if d[k].__name__ not in ("Event", "ParcelChangedEvent"):
                event_categories.append(d[k])
            continue
    except TypeError:
        continue


details = EventDetails(None, None, None, None)
details.changes = Changes(None, None, None)

# Todo: During this setup phase, add CogLand objects into local db
@pytest.fixture
def taxstatus_paid():
    return TaxStatus(
        year="2020",
        paidstatus="PAID",
        tax="473",
        penalty="000",
        interest="000",
        total="473",
        date_paid="6/2/2020",
    )


@pytest.fixture
def taxstatus_unpaid():
    return TaxStatus(
        year="2020",
        paidstatus="UNPAID",
        tax="36894",
        penalty="1845",
        interest="369",
        total="39108",
        date_paid=None,
    )


@pytest.fixture
def taxstatus_balancedue():
    return TaxStatus(
        year="2020",
        paidstatus="BALANCE DUE",
        tax="069",
        penalty="003",
        interest="001",
        total="073",
        date_paid=None,
    )


@pytest.fixture
def taxstatus_none():
    return TaxStatus(
        year="2020",
        paidstatus=None,
        tax="000",
        penalty="000",
        interest="000",
        total="000",
        date_paid=None,
    )


@pytest.fixture
def person1_prop_imap():
    with open(MOCKS + "person1_prop_imap.pickle", "rb") as p:
        return pickle.load(p)


@pytest.fixture
def person1_cecase_imap():
    with open(MOCKS + "person1_cecase_imap.pickle", "rb") as p:
        return pickle.load(p)


@pytest.fixture
def person1_owner_imap():
    with open(MOCKS + "person1_owner_imap.pickle", "rb") as p:
        return pickle.load(p)


@pytest.fixture
def person1_propertyexternaldata_imap():
    with open(MOCKS + "person1_propertyexternaldata_imap.pickle", "rb") as p:
        return pickle.load(p)


class TestEventsTrigger:
    """
    """

    # query_propertyexternaldata_for_changes_and_write_events
    # Mock query_propertyexternaldata_for_changes_and_write_events

    # # Functions to mock
    # db_cursor.execute(select_sql, {"prop_id": prop_id})
    # db_cursor.fetchall()
    # Todo: As of August 2020, condition is a str. Should it be an int?
    # [('{"LASTNAME FIRSTNAME M     "}', '0 N TEST AVE', 'PITTSBURGH PA 15206', 2064, None), [('{"LASTNAME FIRSTNAME M     "}', '0 N TEST AVE', 'PITTSBURGH PA 15206', 2064, None)]

    # @mock.patch(
    #     "pyparcel.dbcursor.fetchall",
    #     return_value=[
    #         ('{"LASTNAME FIRSTNAME M     "}', '0 N TEST AVE', 'PITTSBURGH PA 15206', 2064, None),
    #         ('{"LASTNAME DIFFERENT M     "}', '0 N TEST AVE', 'PITTSBURGH PA 15206', 2064, None)]
    # )
    # def test_DifferentOwner_trigger(self, fetchall):
    #     pass

    @pytest.fixture
    def example_class(self):
        class ExampleClass:
            def __init__(self):
                pass

            def set_value(self):
                self.value = True

        return ExampleClass()

    # # @mock.patch("_events.example_with_classes", return_value=True)
    # def test_example_func(self, example_class):
    #     assert example_with_classes(example_class) == True

    # @mock.patch(
    #     "_events.psycopg2."
    # )
    # def test_DifferentOwner_trigger(self, mocked):
    #     assert query_propertyexternaldata_for_changes_and_write_events(
    #         "PARCEL_ID", 1234, 5678, False, None
    #     ) == True
    #

    def testDifferentStreet_trigger(self):
        pass

    def testDifferentCityStateZip(self):
        pass

    def testDifferentLivingArea(self):
        pass

    def testDifferentCondition(self):
        pass

    def testDifferentTaxStatus(self):
        pass

    def testDifferentTaxCode(self):
        pass


@pytest.fixture()
def test_cursor():
    class mocked_cursor:
        def execute(self, *args, **kwargs):
            return None

        def fetchall(self):
            return (
                ["Old Name", True, True, True, True],
                ["New Name", True, True, True, True],
            )

        def fetchone(self):
            return [
                True,
            ]

    return mocked_cursor()


# def test_query_propertyexternaldata_for_changes_and_write_events(test_cursor):
#     x =  query_propertyexternaldata_for_changes_and_write_events(
#         None, None, None, True, test_cursor
#     )


class TestParse:
    # Todo: pickled objects to fixtures?
    class TestParseTaxFromSoup:
        """ Assert parse_tax_from_soup returns the correct TaxStatus, given a BeautifulSoup object"""

        # Todo: Learn if these tests break if BS4 is
        def test_paid(self, taxstatus_paid):
            with open(MOCKS + "paid.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_paid

        def test_unpaid(self, taxstatus_unpaid):
            with open(MOCKS + "unpaid.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_unpaid

        def test_balancedue(self, taxstatus_balancedue):
            with open(MOCKS + "balancedue.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_balancedue

        def test_none(self, taxstatus_none):
            # Todo: Does the truely represent no taxes, or is it representative of blank data?
            with open(MOCKS + "none.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_none

    class TestParseOwnerFromSoup:
        pass


try:
    conn = psycopg2.connect(
        database="cogdb", user="sylvia", password="c0d3", host="localhost", port="5432"
    )
except psycopg2.OperationalError:

    @contextmanager
    def mocked_conn():
        try:
            yield None
        finally:
            pass

    conn = mocked_conn()


with conn:

    def transaction(func):
        """ transaction is a decorator that allows each unittest to be run in its own transaction
        """

        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            cursor = conn.cursor()
            try:
                cursor.execute("BEGIN;")
                func(*args, **kwargs)
            finally:
                cursor.execute("ROLLBACK;")
                cursor.close()

        return wrapper

    def db_connection_established():
        """ db_connection_established is a flag representing if a database connection could be made.
        """
        if isinstance(conn, psycopg2.extensions.connection):
            return True

    def test_database_connection():
        assert db_connection_established()

    @pytest.mark.skipif(
        not db_connection_established(), reason="Requires a database connection"
    )
    class TestsRequiringADatabaseConnection:
        class TestWrites:
            """ TestWrites tests check that the code write to the database properly.
            """

            @transaction
            def test_property(self, person1_prop_imap):
                with conn.cursor() as cursor:
                    write.property(person1_prop_imap, cursor)

            # # Requires a property id
            # @transaction
            # def test_unit(self):
            #     with conn.cursor() as cursor:
            #         write.unit(unit_imap, cursor)
            #
            # @transaction
            # def test_person(self, person1_owner_imap):
            #     with conn.cursor() as cursor:
            #         write.person(person1_owner_imap, cursor)
            #
            # # def test_connect_property_to_person(self):
            # #     with conn.cursor() as cursor:
            # #         write.connect_property_to_person(prop_id, person_id, cursor)
            #
            # def test_taxstatus(self):
            #     with conn.cursor() as cursor:
            #         write.taxstatus(tax_status, cursor)
            #
            # @transaction
            # def test_propertyexternaldata(self, person1_propertyexternaldata_imap):
            #     with conn.cursor() as cursor:
            #         write.propertyexternaldata(person1_propertyexternaldata_imap, cursor)

        class TestEventCategories:
            """ Ensures events in _events.py share the same attributes of their counterpart in the database.
            """

            @pytest.mark.parametrize("event", event_categories)
            def test_name_integrity(self, event):
                """ Compares the class's name to the database's event category's title.
                """
                with conn.cursor() as cursor:
                    instance = event(details)
                    info = {}
                    info["column"] = event.__name__
                    info["category_id"] = instance.category_id
                    select_sql = """
                            SELECT %(column)s
                            FROM eventcategory
                            WHERE categoryid = %(category_id)s;
                            """
                    cursor.execute(select_sql, info)
                    row = cursor.fetchone()
                    assert event.__name__ == row[0]

            # Todo: Refactor and do a little currying
            @pytest.mark.parametrize("event", event_categories)
            def test_active_integrity(self, event):
                """ Compares the class's default active status to the database's.
                """
                with conn.cursor() as cursor:
                    instance = event(details)
                    info = {}
                    info["column"] = instance.active
                    info["category_id"] = instance.category_id
                    select_sql = """
                            SELECT %(column)s
                            FROM eventcategory
                            WHERE categoryid = %(category_id)s;
                            """
                    cursor.execute(select_sql, info)
                    row = cursor.fetchone()
                    assert instance.active == row[0]


if __name__ == "__main__":
    pytest.main()
