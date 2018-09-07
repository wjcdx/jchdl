module FullAdder(
    output wire sum,
    output wire cout,
    input wire  a,
    input wire  b,
    input wire  cin
);

    wire w7;
    wire w6;
    wire w8;

    and(w6, w7, cin);
    xor(sum, w7, cin);
    and(w8, a, b);
    xor(w7, a, b);
    xor(cout, w6, w8);

endmodule