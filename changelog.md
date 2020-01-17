# Changelog

## v0.2.0 (2020-01-17)

- Abstract classes were replaced with lightweight interfaces for more flexibility. This shouldn't affect most users, but it does break serialization compatibility
- IPv4Subnet mask length is now an `int` instead of an annoying `byte`, making it consistent with the IPv6 counterpart
- Removed deprecated `between` function
- Length type of ranges (`Long` for IPv4, `BigInteger` for IPv6) is now a generic parameter of the `IPRange`/`IPSubnet` interface. This should make writing generic code that uses `IPRange` or `IPSubnet` easier without affecting users of the concrete classes.
- New methods in `IPv4Address`, `IPv6Address`:	
    - Logical operators: `and`, `not`, `or`, `xor`
    - `toRange`, `toSubnet`
    - `distanceTo`, `isAdjacentTo`
- New methods in `IPv4Range`, `IPv6Range`, `IPv4Subnet`, `IPv6Subnet`:
    - Range extension methods: `withFirst`, `withLast`
    - `intLength`
    - `merge` for merging several ranges into a minimal list of non-overlapping ranges
     

## v0.1.2 (2019-07-02)

- Range and Subnet instances that contain the same addresses are now equal to each other despite being different classes.

## v0.1.1 (2019-07-01)

- The deprecated the `between()` range builder, replaced it with a new `parse()` function that accepts subnets (1.2.3.0/24) , ranges (1.2.3.4-1.2.3.5) and single addresses (1.2.3.4).

## v0.1.0 (2018-05-27)

- Initial release
