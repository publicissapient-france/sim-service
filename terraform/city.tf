# Configure the AWS Provider

provider "aws" {
    access_key = "${var.access_key}"
    secret_key = "${var.secret_key}"
    region = "eu-west-1"
}

resource "aws_security_group" "simservice-sg" {
    name = "simservice-sg"
    description = "Allow all inbound traffic"

    ingress {
        from_port = 0
        to_port = 65535
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }
}

resource "aws_instance" "factory" {
    count = 0
    key_name = "insecure-sim-service"
    ami = "ami-f8af0d8f"
    instance_type = "m1.small"
    tags {
        Name = "SimService node#${count.index}"
        Short = "sim${count.index}"
    }
    subnet_id = "${aws_subnet.simservice-net.id}"
    connection {
        user = "ubuntu"
        key_file = "insecure-sim-service.pem"
    }

    provisioner "remote-exec" {
        inline = [
        "sudo apt-get -y update",
        "sudo apt-get -y install git",
        ]
    }

}

resource "aws_instance" "core" {
    key_name = "insecure-sim-service"
    ami = "ami-f8af0d8f"
    instance_type = "m1.small"
    tags {
        Name = "SimService core"
    }
    subnet_id = "${aws_subnet.simservice-net.id}"
}

resource "aws_subnet" "simservice-net" {
  vpc_id = "vpc-6648a303"
  cidr_block = "172.30.3.0/24"
  map_public_ip_on_launch = true
  availability_zone = "eu-west-1b"
  tags {
          Name = "SimService subnet"
      }
}

# DNS
resource "aws_route53_zone" "primary" {
   name = "auffredou.fr"
}

variable "domains" {
    default = {
        sim0 = "sim0"
        sim1 = "sim1"
        sim2 = "sim2"
        sim3 = "sim3"
    }
}

#resource "aws_route53_record" "factory-record" {
#    count = 3
#    zone_id = "${aws_route53_zone.primary.zone_id}"
#    zone_id = "Z28O5PDK1WPCSR"
#    name = "${lookup(var.domains,count.index)}"
#    type = "A"
#    ttl = "300"
#    records = ["${aws_instance.factory.${count.index}.public_ip}"]
#}