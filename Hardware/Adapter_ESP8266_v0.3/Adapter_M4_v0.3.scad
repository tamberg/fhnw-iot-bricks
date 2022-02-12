// CC BY-SA, @tamberg

$fn = 36;

r = 2.54;
w = 51;
h = 33.5;

hull() {
  translate([r, r]) {
    translate([0, 0]) {
      circle(r);
    }
    translate([w - 2 * r, 0]) {
      circle(r);
    }
    translate([w - 2 * r, h - 2 * r]) {
      circle(r);
    }
    translate([0, h - 2 * r]) {
      circle(r);
    }
  }
}